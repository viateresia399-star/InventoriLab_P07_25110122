package tampilan;

import koneksi.KoneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class FormBarang extends JFrame {
    private static final long serialVersionUID = 1L;
    private final JTextField txtKode = new JTextField(20);
    private final JTextField txtNama = new JTextField(20);
    private final JComboBox<Kategori> cmbKategori = new JComboBox<Kategori>();
    private final JTextField txtJumlah = new JTextField("0", 20);
    private final JTextField txtKeterangan = new JTextField(20);
    private final JButton btnSimpan = new JButton("Simpan Barang");
    private final JButton btnBersihkan = new JButton("Bersihkan Form");
    private final JButton btnMuat = new JButton("Muat Ulang Data");
    private final JLabel lblStatus = new JLabel("Menyiapkan aplikasi...");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Kode", "Nama Barang", "Kategori", "Jumlah", "Keterangan"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        @Override
        public Class<?> getColumnClass(int column) {
            return column == 3 ? Integer.class : String.class;
        }
    };
    private final JTable tabel = new JTable(model);
    private boolean sibuk = false;

    private static class Kategori {
        private final int id;
        private final String nama;
        Kategori(int id, String nama) {
            this.id = id;
            this.nama = nama;
        }
        @Override
        public String toString() {
            return nama;
        }
    }

    private static class DataTampilan {
        private final List<Kategori> kategori = new ArrayList<Kategori>();
        private final List<Object[]> barang = new ArrayList<Object[]>();
    }

    public FormBarang() {
        super("Inventori Laboratorium | P07 - CREATE dan READ");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (sibuk) {
                    JOptionPane.showMessageDialog(FormBarang.this,
                            "Tunggu sampai proses database selesai.");
                } else {
                    dispose();
                }
            }
        });
        susunTampilan();
        pasangAksi();
        pack();
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);
        muatData("");
    }

    private void susunTampilan() {
        JPanel utama = new JPanel(new BorderLayout(12, 12));
        utama.setBorder(new EmptyBorder(18, 18, 18, 18));
        utama.setBackground(new Color(238, 243, 249));
        setContentPane(utama);

        JLabel judul = new JLabel("INVENTORI LABORATORIUM");
        judul.setFont(new Font("SansSerif", Font.BOLD, 24));
        judul.setForeground(new Color(24, 49, 83));
        JLabel subjudul = new JLabel("Tambah barang dan lihat data dari database");
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        header.add(judul, BorderLayout.NORTH);
        header.add(subjudul, BorderLayout.SOUTH);
        utama.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Data Barang Baru"));
        tambahBaris(form, 0, "Kode barang", txtKode);
        tambahBaris(form, 1, "Nama barang", txtNama);
        tambahBaris(form, 2, "Kategori", cmbKategori);
        tambahBaris(form, 3, "Jumlah tersedia", txtJumlah);
        tambahBaris(form, 4, "Keterangan (boleh kosong)", txtKeterangan);
        JPanel tombol = new JPanel();
        tombol.add(btnSimpan);
        tombol.add(btnBersihkan);
        tombol.add(btnMuat);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 5;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 6, 8, 6);
        form.add(tombol, g);

        tabel.setRowHeight(26);
        tabel.setAutoCreateRowSorter(true);
        tabel.setFillsViewportHeight(true);
        tabel.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setPreferredSize(new Dimension(860, 230));
        scroll.setBorder(BorderFactory.createTitledBorder("Daftar Barang - Hasil SELECT"));
        JPanel tengah = new JPanel(new BorderLayout(10, 10));
        tengah.setOpaque(false);
        tengah.add(form, BorderLayout.NORTH);
        tengah.add(scroll, BorderLayout.CENTER);
        utama.add(tengah, BorderLayout.CENTER);
        utama.add(lblStatus, BorderLayout.SOUTH);
    }

    private void tambahBaris(JPanel panel, int baris, String teks, JComponent isi) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = baris;
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        panel.add(new JLabel(teks), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(isi, g);
    }

    private void pasangAksi() {
        btnSimpan.addActionListener(e -> simpanBarang());
        btnBersihkan.addActionListener(e -> bersihkanForm());
        btnMuat.addActionListener(e -> muatData(""));
    }

    private void aturSibuk(boolean nilai) {
        sibuk = nilai;
        txtKode.setEnabled(!nilai);
        txtNama.setEnabled(!nilai);
        txtJumlah.setEnabled(!nilai);
        txtKeterangan.setEnabled(!nilai);
        cmbKategori.setEnabled(!nilai);
        btnSimpan.setEnabled(!nilai && cmbKategori.getItemCount() > 0);
        btnBersihkan.setEnabled(!nilai);
        btnMuat.setEnabled(!nilai);
    }

    private void bersihkanForm() {
        txtKode.setText("");
        txtNama.setText("");
        txtJumlah.setText("0");
        txtKeterangan.setText("");
        if (cmbKategori.getItemCount() > 0) {
            cmbKategori.setSelectedIndex(0);
        }
        txtKode.requestFocusInWindow();
    }

    private void muatData(final String pesanSimpan) {
        if (sibuk) {
            return;
        }
        Kategori pilihanLama = (Kategori) cmbKategori.getSelectedItem();
        final Integer idTerpilih = pilihanLama == null ? null : pilihanLama.id;
        aturSibuk(true);
        lblStatus.setText(pesanSimpan + "Memuat data dari database...");
        new SwingWorker<DataTampilan, Void>() {
            @Override
            protected DataTampilan doInBackground() throws SQLException {
                DataTampilan data = new DataTampilan();
                try (Connection con = KoneksiDB.getConnection()) {
                    String sqlKategori = "SELECT id_kategori, nama_kategori "
                            + "FROM kategori ORDER BY nama_kategori";
                    try (PreparedStatement ps = con.prepareStatement(sqlKategori);
                            ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            data.kategori.add(new Kategori(rs.getInt("id_kategori"),
                                    rs.getString("nama_kategori")));
                        }
                    }
                    String sqlBarang = "SELECT b.kode_barang, b.nama_barang, "
                            + "k.nama_kategori, b.jumlah_tersedia, b.keterangan "
                            + "FROM barang b LEFT JOIN kategori k "
                            + "ON b.id_kategori = k.id_kategori ORDER BY b.kode_barang";
                    try (PreparedStatement ps = con.prepareStatement(sqlBarang);
                            ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String ket = rs.getString("keterangan");
                            data.barang.add(new Object[]{
                                rs.getString("kode_barang"), rs.getString("nama_barang"),
                                rs.getString("nama_kategori"), rs.getInt("jumlah_tersedia"),
                                ket == null ? "" : ket
                            });
                        }
                    }
                }
                return data;
            }
            @Override
            protected void done() {
                try {
                    DataTampilan data = get();
                    cmbKategori.removeAllItems();
                    for (Kategori k : data.kategori) {
                        cmbKategori.addItem(k);
                    }
                    if (idTerpilih != null) {
                        cmbKategori.setSelectedIndex(-1);
                        for (int i = 0; i < cmbKategori.getItemCount(); i++) {
                            if (cmbKategori.getItemAt(i).id == idTerpilih.intValue()) {
                                cmbKategori.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                    model.setRowCount(0);
                    for (Object[] baris : data.barang) {
                        model.addRow(baris);
                    }
                    String status = pesanSimpan + "Data terbaru: " + model.getRowCount()
                            + " barang.";
                    if (data.kategori.isEmpty()) {
                        status += " Kategori kosong: laporkan kepada dosen.";
                    }
                    lblStatus.setText(status);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    gagalMuat(pesanSimpan, e);
                } catch (ExecutionException e) {
                    gagalMuat(pesanSimpan, e.getCause());
                } finally {
                    aturSibuk(false);
                }
            }
        }.execute();
    }

    private void gagalMuat(String pesanSimpan, Throwable sebab) {
        lblStatus.setText(pesanSimpan + "Gagal memuat. Tabel belum diperbarui; klik Muat Ulang Data.");
        tampilkanError(pesanSimpan + "Pembacaan data gagal.", sebab);
    }

    private void simpanBarang() {
        if (sibuk) {
            return;
        }
        final String kode = txtKode.getText().trim();
        final String nama = txtNama.getText().trim();
        final String ket = txtKeterangan.getText().trim();
        final Kategori kategori = (Kategori) cmbKategori.getSelectedItem();
        if (kode.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode dan nama barang wajib diisi.");
            return;
        }
        if (kode.length() > 20 || nama.length() > 150 || ket.length() > 255) {
            JOptionPane.showMessageDialog(this,
                    "Batas karakter: kode 20, nama 150, keterangan 255.");
            return;
        }
        if (kategori == null) {
            JOptionPane.showMessageDialog(this, "Kategori belum tersedia. Klik Muat Ulang Data.");
            return;
        }
        final int jumlah;
        try {
            jumlah = Integer.parseInt(txtJumlah.getText().trim());
            if (jumlah < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Jumlah harus bilangan bulat 0 sampai 2147483647, tanpa titik atau koma.");
            return;
        }
        aturSibuk(true);
        lblStatus.setText("Menyimpan barang...");
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws SQLException {
                String sql = "INSERT INTO barang "
                        + "(kode_barang, nama_barang, id_kategori, jumlah_tersedia, keterangan) "
                        + "VALUES (?, ?, ?, ?, ?)";
                try (Connection con = KoneksiDB.getConnection();
                        PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, kode);
                    ps.setString(2, nama);
                    ps.setInt(3, kategori.id);
                    ps.setInt(4, jumlah);
                    ps.setString(5, ket);
                    return ps.executeUpdate();
                }
            }
            @Override
            protected void done() {
                boolean berhasil = false;
                try {
                    int baris = get();
                    if (baris == 1) {
                        berhasil = true;
                        bersihkanForm();
                        JOptionPane.showMessageDialog(FormBarang.this,
                                "Barang berhasil disimpan. Klik OK untuk memuat ulang daftar.");
                    } else {
                        lblStatus.setText("Hasil simpan tidak sesuai. Periksa database sebelum mengulang.");
                        JOptionPane.showMessageDialog(FormBarang.this,
                                "Jumlah baris terdampak: " + baris + ". Periksa database dahulu.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    gagalSimpan(e);
                } catch (ExecutionException e) {
                    gagalSimpan(e.getCause());
                } finally {
                    aturSibuk(false);
                }
                if (berhasil) {
                    muatData("Simpan berhasil. ");
                }
            }
        }.execute();
    }

    private void gagalSimpan(Throwable sebab) {
        lblStatus.setText("Simpan belum terkonfirmasi. Periksa database jika koneksi terputus.");
        tampilkanError("Penyimpanan tidak berhasil dikonfirmasi.", sebab);
    }

    private void tampilkanError(String konteks, Throwable sebab) {
        String pesan = konteks;
        if (sebab instanceof SQLException) {
            SQLException e = (SQLException) sebab;
            if (e.getErrorCode() == 1062) {
                pesan += "\nKode barang sudah digunakan. Gunakan kode lain.";
            } else if (e.getErrorCode() == 1142) {
                pesan += "\nHak akun belum sesuai. Periksa GRANT SELECT dan INSERT.";
            } else if (e.getErrorCode() == 1452) {
                pesan += "\nKategori tidak ditemukan. Muat ulang kategori terlebih dahulu.";
            } else {
                pesan += "\nPeriksa MySQL, konfigurasi koneksi, driver, dan struktur tabel.";
            }
            pesan += "\nSQLState: " + e.getSQLState() + "; kode: " + e.getErrorCode();
        } else {
            pesan += "\nJenis gangguan: " + sebab.getClass().getSimpleName();
        }
        JOptionPane.showMessageDialog(this, pesan, "Informasi Database", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormBarang().setVisible(true));
    }
}